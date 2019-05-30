@fmeaFailure
@DiskFull

# This is an example scenario on how to use gherkin/cucumber based DSL to inject failures

Feature: Amazon EC2 Volume Full

  @diskFullSSH
  Scenario Outline: EC2 instance filling up volume via SSH
    Given EC2 <ec2Name>
    And ALB <elbName>
    And CloudWatch Alarm <alarmName>
    When <VolumeType> disk full with <size> GB on <instanceCount> instance
    Then wait for <wait1> minute
    And assertEC2 healthy host count = <expected-count1>
    And assertCW alarm = <state1>
    And wait for <wait2> minute
    And assertCW alarm = <state2>
    And assertEC2 healthy host count = <expected-count2>

  @dev
    Examples:
      | ec2Name     | alarmName                  | elbName     | VolumeType | size | instanceCount | wait1 | wait2 | state1  | state2 | expected-count1 | expected-count2 |
      | "hello-dev" | "hello-dev-UnHealthyHosts" | "hello-dev" | "root"     | 10   | 1             | 6     | 5     | "ALARM" | "OK"   | 5               | 6               |
      | "hello-dev" | "hello-dev-UnHealthyHosts" | "hello-dev" | "app"      | 100  | 1             | 6     | 5     | "ALARM" | "OK"   | 5               | 6               |

  @diskFullSSM
  Scenario Outline: EC2 instance filling up volume via SSM
    Given EC2 <ec2Name>
    And ALB <elbName>
    And CloudWatch Alarm <alarmName>
    When SSM <VolumeType> disk full with <size> GB on <instanceCount> instance
    Then wait for <wait1> minute
    And assertEC2 healthy host count = <expected-count1>
    And assertCW alarm = <state1>
    And wait for <wait2> minute
    And assertCW alarm = <state2>
    And assertEC2 healthy host count = <expected-count2>

  @dev
    Examples:
      | ec2Name     | alarmName                  | elbName     | VolumeType | size | instanceCount | wait1 | wait2 | state1  | state2 | expected-count1 | expected-count2 |
      | "hello-dev" | "hello-dev-UnHealthyHosts" | "hello-dev" | "root"     | 10   | 1             | 6     | 5     | "ALARM" | "OK"   | 5               | 6               |
      | "hello-dev" | "hello-dev-UnHealthyHosts" | "hello-dev" | "app"      | 100  | 1             | 6     | 5     | "ALARM" | "OK"   | 5               | 6               |


  @albRamDiskFull
  Scenario Outline: RAM disk Full
    Given ALB <albName>
    When SSM RAM disk full with <size> GB on <instanceCount> instance
    Then wait for <wait1> minute
    And assertEC2 healthy host count = <expected-count1>
    And recover
    And assertEC2 healthy host count = <expected-count2>

  @dev
    Examples:
      | albName     | instanceCount | wait1 | expected-count1 | expected-count2 | size |
      | "hello-dev" | 1             | 8     | 9               | 9               | 4    |
