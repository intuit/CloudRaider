# Cloud-Raider-Java-Client

This is example project to show integration of CloudRaider library with Java/JUnit tests.

## Getting Started

Run the maven command to make sure you have all the dependencies 
```
mvn clean install -DskipTests
```

if the build is success, now you can start configuring the client and add test cases

under src/test/resources/config.properties add the following properties

```
aws.ec2.privateKeyPath=/path/to/pem file <myaccount.pem>
aws.ec2.bastionIp=<IP or DNS Name for bastion>
aws.region=us-west-2 or <any other region>
aws.ec2.username=ec2-user
aws.profile=<pre-prod provile>

```
or if you want to add the secrets here and don't use the profile
```
aws.accessKeyId=<AWS Access Key>
aws.secretKey=<AWS Secret>
```
if you are using passphrase

```
aws.ec2.privateKeyPassPhrase=
```
## How to add new Test case?

Let's look an example EC2FMEATest class that invokes termination of EC2 Instance.

In the before method we have to create an object for EC2Raider to allow us to access all methods in the raider
and also create AmazonEC2 client object which will have the credentials object to access AWS API's as below
```
@BeforeTest
    public void setup() {
        ec2Raider = new EC2RaiderImpl();
         ec2 = (new EC2Delegator()).getEc2();

    }
```
Then add basic Test case and run it from IDE or commandline

````
 @Test
    public void terminateEC2Intance() throws Exception
    {
    
        //name of the instance to be terminated     
        String tag = "<InstanceName>";
        
        //this will return the number of instance with the same name you provided
        List<String> instanceIds = ec2Raider.getInstancesIdsForOneTag(tag);
        int runningInstancesBefore =  instanceIds.size();
        
        //get first instance in the list and terminated
        String instanceId = instanceIds.get(0);
        ec2Raider.terminateEc2InstancesById(instanceId);
        
        // wait some time for the instance to mark as terminated
        Thread.sleep(210000);
        
        //check the status of the instance
        String actual = ec2Raider.getInstanceStatus(instanceId);
        
        //Assert if the instance get terminated
        Assert.assertEquals(actual,"terminated");
        
        //Assert if the ASG worked and another instance came online
        Assert.assertEquals(ec2Raider.getInstancesIdsForOneTag(tag).size(), runningInstancesBefore);

    }
````
