@fmeaFailure
@ProcessTermination

# This is an example scenario on how to use gherkin/cucumber based DSL to inject failures

Feature: Amazon EC2 instance process termination.


  Scenario Outline: Process termination on EC2 instances, validation & recovery
    Given EC2 <ec2Name>
    And ALB <elbName>
    And  CloudWatch Alarm <alarmName>
    When terminate process  <processName> on <instanceCount> instance
    Then wait for <wait1> minute
    And assertEC2 healthy host count = <expected-count1>
    And assertCW alarm = <state1>
    And recover
    And wait for <wait2> minute
    And assertCW alarm = <state2>
    And assertEC2 healthy host count = <expected-count2>

  @dev
    Examples:
      | ec2Name     | elbName      | alarmName                  | instanceCount | processName |  | wait1 | wait2 | state1  | state2 | expected-count1 | expected-count2 |
      | "hello-dev" | "hello-dev " | "hello-dev-UnHealthyHosts" | 1             | "nginx"     |  | 4     | 4     | "ALARM" | "OK"   | 5               | 6               |
      | "hello-dev" | "hello-dev " | "hello-dev-UnHealthyHosts" | 1             | "java"      |  | 4     | 4     | "ALARM" | "OK"   | 5               | 6               |


  @ProcessTerminationInAZ
  Scenario Outline: Process termination on EC2 instances in a given availability zone, validation & recovery
    Given EC2 <ec2Name>
    And ALB <elbName>
    And  CloudWatch Alarm <alarmName>
    When terminate process  <processName> on <instanceCount> instance in zone <zone-id>
    Then wait for <wait1> minute
    And assertEC2 healthy host count = <expected-count1>
    And assertCW alarm = <state1>
    And recover
    And wait for <wait2> minute
    And assertCW alarm = <state2>
    And assertEC2 healthy host count = <expected-count2>

  @dev
    Examples:
      | ec2Name     | elbName     | alarmName                  | instanceCount | processName | wait1 | wait2 | state1  | state2 | expected-count1 | expected-count2 | zone-id      |
      | "hello-dev" | "hello-dev" | "hello-dev-UnHealthyHosts" | 2             | "nginx"     | 4     | 4     | "ALARM" | "OK"   | 4               | 6               | "us-west-2c" |
      | "hello-dev" | "hello-dev" | "hello-dev-UnHealthyHosts" | 2             | "java"      | 4     | 4     | "ALARM" | "OK"   | 4               | 6               | "us-west-2c" |

  @ProcessTerminationSSM
#    Expects SSM to be pre-configured
  Scenario Outline: Process termination on EC2 by using AWS Simple System Manager (SSM)
    Given ALB <albName>
    And CloudWatch Alarm <alarmName>
    When SSM terminate process  <processName> on <numInstance> instance
    Then wait for <wait1> minute
    And assertCommand execution status = <commandStatus>
    And assertEC2 healthy host count = <count1>
    And assertCW alarm = <state1>
    And assertCW alarm <alarm2> = <state1>
    And recover
    And wait for <wait2> minute
    And assertCW alarm = <state2>
    And assertEC2 healthy host count = <count2>

  @dev
    Examples:
      | albName     | alarmName                  | processName | wait1 | wait2 | count1 | count2 | state1  | state2 | numInstance | commandStatus | alarm2                             |
      | "hello-dev" | "hello-dev-UnHealthyHosts" | "nginx"     | 2     | 5     | 0      | 3      | "ALARM" | "OK"   | 3           | "Success"     | "hello-dev-HTTPCode_ELB_5XX_Count" |
      | "hello-dev" | "hello-dev-UnHealthyHosts" | "java"      | 2     | 5     | 0      | 3      | "ALARM" | "OK"   | 3           | "Success"     | "hello-dev-HTTPCode_ELB_5XX_Count" |

