package example;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;



public class Hello implements RequestHandler<RequestClass, ResponseClass> {
//{"message":"This is good bot","facebookid":"1086532071394537"}
	//{"message":"recommend","facebookid":"1086532071394537"}
   
	@Override
	public ResponseClass handleRequest(RequestClass input, Context context) {
	
		String message = input.getMessage();
		String facebookId = input.getFacebookid();
		double sentiment = 0;
		String theResponse = "";
		
		AWSCredentials creds = new BasicAWSCredentials("AKIAJWHDLGQPUCZXF22Q","RDkDqLW6tnieSoSmLm2Bowg+XruTlUxlaIsuhGar");
		AmazonS3Client s3Client = new AmazonS3Client(creds);
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        s3Client.setRegion(usWest2);
        
        S3Object object = s3Client.getObject(new GetObjectRequest("hello-lambda-bucket-test", "SentiWordNet.txt"));
        InputStream objectDataStream = object.getObjectContent();
        
        SentiwordNet sword = new SentiwordNet(objectDataStream);
		sentiment = sword.calcSentimentForSentence(message);
		
		AmazonDynamoDBClient dyndbclient = new AmazonDynamoDBClient(creds);
		dyndbclient.setRegion(usWest2);
		
		 /* Check message if its normal message save it in dynamoDB, return "" but 
		 * if is equals word "Recommend", then return avg sentiment of previous 5 messages
		 */ 
		
		if(!message.equalsIgnoreCase("recommend")){
			String tableName = "AWSBigData";
			DynamoDB dynamoDB = new DynamoDB(dyndbclient);
			Table table = dynamoDB.getTable(tableName);
			
			// Build the item
			//primary key should be unique so , set it as timestamp
			Item item = new Item()
			    .withPrimaryKey("id", new Date().getTime())
			    .withString("facebookid", facebookId)
			    .withString("message", message)
			    .withDouble("sentiment", sentiment);
			    
			// Write the item to the table 
			//PutItemOutcome outcome =
			table.putItem(item);
			theResponse = "NA";
		}			
		else{
			DynamoDBMapper mapper = new DynamoDBMapper(dyndbclient);
			Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
			 	//eav.put(":val1", new AttributeValue().withS("1"));
		        eav.put(":val2", new AttributeValue().withS(facebookId));
	        
		        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
		            .withFilterExpression("facebookid = :val2")
		            .withExpressionAttributeValues(eav)
		        	.withLimit(1);//limit doesn't work
		        
		        List<MessageSentiment> scanResult = mapper.scan(MessageSentiment.class, scanExpression);
		        
		        
		        List<MessageSentiment> sortedScanResult = new ArrayList<>();
		        Iterator<MessageSentiment> ite=scanResult.iterator();
		        int count = 0;
		        while(ite.hasNext()){
		        	MessageSentiment ms = ite.next();
		        	if(count == 0){
		        		sortedScanResult.add(ms);
		        		count++;
		        		continue;
		        	}
		        	
		        	theloop:for(int i=0;i<sortedScanResult.size();i++){
		        		MessageSentiment ims = sortedScanResult.get(i);
		        		if(ims.getId() < ms.getId()){
		        			sortedScanResult.add(i, ms);
		        			break theloop;
		        		}
		        	}
		        	if(sortedScanResult.size() > 10){
		        		sortedScanResult.removeAll(sortedScanResult.subList(10, sortedScanResult.size()));
	        		}
		        	
		        }
		        
		        
		        
		        double averageSentiment = 0;
		        for(MessageSentiment ms:sortedScanResult){
		        	//theResponse += ms.getMessage()+"\\n";
			        averageSentiment += ms.getSentiment();
		        }
		        
		        /*double averageSentiment = 0;
		        
		        
		        int count = 0;
		        for(int i = scanResult.size()-1;i<0;i--){
		        //for(MessageSentiment ms:scanResult){
		        	theResponse += scanResult.get(i).getMessage()+"\\n";
			        averageSentiment += scanResult.get(i).getSentiment();
			        count++;
			        if(count >= 10)
			        {
			        	break;
			        }
		        }*/
		        
		        averageSentiment = averageSentiment/sortedScanResult.size();
		        String result = "";
		        if(averageSentiment < 0.3 && averageSentiment > -0.2)
		        {
		        	result = "You have neutral response me. I will try to make it more better, Thank you";
		        } else if(averageSentiment > 0.3) {
		        	result="Your interaction with me is positive, I will recommend you some videos:";
		        } else {
		        	result="It seems you are not happy with me, I will try harder next time to make your experience a better one, Thank you";
		        }
		        theResponse = result+"";
		}
		ResponseClass rClass = new ResponseClass();
		rClass.setMessage(theResponse);
		return rClass;
	}
}
