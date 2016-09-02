package example;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="AWSBigData")
public class MessageSentiment {
	
	private long id;
	private String facebookid;
	private String message;
	private double sentiment;
	 @DynamoDBHashKey(attributeName="id")
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	@DynamoDBAttribute(attributeName="facebookid")
	public String getFacebookid() {
		return facebookid;
	}
	public void setFacebookid(String facebookid) {
		this.facebookid = facebookid;
	}
	
	@DynamoDBAttribute(attributeName="message")
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	@DynamoDBAttribute(attributeName="sentiment")
	public double getSentiment() {
		return sentiment;
	}
	public void setSentiment(double sentiment) {
		this.sentiment = sentiment;
	}

}
