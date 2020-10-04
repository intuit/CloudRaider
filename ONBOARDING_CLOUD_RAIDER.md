# Onboarding to Cloud Raider

Follow these 4 simple steps to run FMEA on your any service/infrastructure on AWS using Cloud Raider cucumber client:

1. Cloud Raider runs FMEA tests on your AWS account. First step to run FMEA tests is to setup configuration of AWS account on which you want to execute FMEA test suite. Specify the correct value of AWS configuration parameters in configuration file `cucumber-example/src/test/resources/config.properties`.

2. Feature files are used to execute FMEA scenarios and test resiliency of the service infrastructure. The example of feature files are mentioned [here](https://github.com/intuit/CloudRaider/tree/master/cucumber-example/src/test/features). These includes simulation of wide variety of failure scenarios like termination of ec2 instance, increase in CPU utilisation, blocking of outbound ports, DB failure, etc. You can choose any of these features or even add a feature of your own using the available step definition and simulate your own failure scenario.

3. Mention the feature file tags you want to execute as part of your test suite in CloudRaider test class file under cucumber options tags parameter [here](https://github.com/intuit/CloudRaider/blob/master/cucumber-example/src/test/java/com/intuit/cloudraider/client/cucumber/ApplicationLoadBalancerCukeTest.java#L23).

4. Run your entire test suite using the command `mvn test`. This will run all your test scenarios marked with the tags above and produce a summary of test results as maven surefire reports.


## References

- Cucumber Feature Files: [here](https://cucumber.io/docs/gherkin/reference/)
- Cucumber Tags: [here](https://cucumber.io/docs/cucumber/api/#tags)