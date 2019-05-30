@fmeaFailure
@TerminateInstances

# This is an example scenario on how to use gherkin/cucumber based DSL to inject failures

Feature: Amazon EC2 instance termination


  Scenario Outline: EC2 Instance termination and assertions
    Given EC2 <ec2Name>
    And ALB <elbName>
    When terminate <instanceCount> instance
    Then wait for <wait1> minute
    And assertEC2 healthy host count = <expected-count1>
    And wait for <wait2> minute
    And assertEC2 healthy host count = <expected-count2>

  @dev
    Examples:
      | ec2Name     | elbName     | instanceCount | wait1 | wait2 | expected-count1 | expected-count2 |
      | "hello-dev" | "hello-dev" | 1             | 3     | 5     | 2               | 3               |


  Scenario Outline: Terminate all instances
    Given EC2 <ec2Name>
    And ALB <elbName>
    When terminate all instances
    Then wait for <wait1> minute
    And assertEC2 healthy host count = <expected-count1>
    And wait for <wait2> minute
    And assertEC2 healthy host count = <expected-count2>

  @dev
    Examples:
      | ec2Name     | elbName     | wait1 | wait2 | expected-count1 | expected-count2 |
      | "hello-dev" | "hello-dev" | 3     | 5     | 2               | 3               |