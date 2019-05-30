@fmeaFailure
@CPUSpike

# This is an example scenario on how to use gherkin/cucumber based DSL to inject failures

Feature: Amazon EC2 instance CPU Spike.

  Scenario Outline: EC2 instance CPU spike and Recovery
    Given EC2 <ec2Name>
    And ALB <elbName>
    And CloudWatch Alarm <alarmName>
    When CPU spike on <instanceCount> instances for <coresCount> cores
    Then wait for <wait1> minute
    And assertEC2 healthy host count = <expected-count1>
    And assertCW alarm = <state1>
    And recover
    And wait for <wait2> minute
    And assertEC2 healthy host count = <expected-count2>
    And assertCW alarm = <state2>

  @dev
    Examples:
      | ec2Name     | alarmName                  | elbName     | coresCount | instanceCount | wait1 | state1  | expected-count1 | wait2 | state2 | expected-count2 |
      | "hello-dev" | "hello-dev-UnHealthyHosts" | "hello-dev" | 4          | 1             | 4     | "ALARM" | 5               | 4     | "OK"   | 6               |
