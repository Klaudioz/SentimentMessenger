There are 3 sources for data.
We can consider the sentiwordtext as the training set data, We have altered the sentiwordtext in several places for to make more suitable for out purpose. It is stored in Amazon S3, to make it accessible from AWS lambda.
The test set here is the message the user sends to the messenger. We compare the phrase and word again set in sentiwordtext to get the sentiment value of the message and process it.
The messages and the calculated sentiment is saved in the dynamodb along with userid, so later it can be scanned easily to get the particular users data.

There are lots of tools involved in this project
Facebook Messenger bot: The tool which is used to interact with user and get the input data to calculate the sentiment.
Stamplay: A online tool used to get the message from bot and perform the AWS lambda function. After the lambda function return result, carryout corresponding reply.
AWS lambda: Functionality provided by Amazon web services, by the means of which we can perform java code in Amazon server without having to get much server time.
Sentiwordnet: The trained set text file that contains the trained set with the stemmed words and their corresponding sentiment value. 
AWS DynamoDb: The NOSql database by Amazon, where we store message, its value and user id
AWS S3: The Amazon storeage facility, where the sentiwordnet is stored. Lambda uses this service to get sentiwordnet.txt

The value is then measured against the values set for positive, negative, and neutral response. The messages are all saved with their corresponding sentiment value.
Certain trigger word currently "recommend" will get the data from the database, and calculate the average for the messages and  return proper phrase to send to user. Stamplay sends the message to user

WordNet is lexical database which groups the synonyms of words namely, synsets. SENTIWORDNET 3.0 glosses from the Princeton WordNet Gloss Corpus, according to which a gloss is actually a sequence of WORDNET synsets, thus based on "bag of synsets" model, unlike the "bag of words" approach in previous version. 
Basically how it populates its data is, it gets positive and negative synsets for  a synset within certain k distance from itself. The glosses are used to classify the positive,negative or neutral rating for the synset.
http://sentiwordnet.isti.cnr.it/

