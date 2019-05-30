@fmeaALBFailure
@fmeaFailure


# This is an example scenario on how to use gherkin/cucumber based DSL to inject failures

Feature: ALB Failures


  @albHealthCheckFailure
  Scenario Outline: Health Check corruption at ALB
    Given ALB <albName>
    And CloudWatch Alarm <alarmName>
    When LB corrupt HealthChecks
    Then wait for <wait1> minute
    And assertEC2 healthy host count = <count1>
    And assertCW alarm = <state1>
    And LB unCorrupt HealthChecks
    And wait for <wait2> minute
    And assertEC2 healthy host count = <count2>
    And assertCW alarm = <state2>

  @dev
    Examples:
      | albName     | wait1 | wait2 | count1 | count2 | state1  | state2 | alarmName                  |
      | "hello-dev" | 3     | 3     | 0      | 1      | "ALARM" | "OK"   | "hello-e2e-UnHealthyHosts" |


  @albDetatchInstances
  Scenario Outline: Detach Instances from Load Balancer
    Given ALB <albName>
    When detach <instanceCount> instances from loadbalancer
    Then wait for <wait1> minute
    And assertEC2 healthy host count = <expected-count1>
    And attach unregistered instances
    And wait for <wait2> minute
    And assertEC2 healthy host count = <expected-count2>

  @dev
    Examples:
      | albName     | instanceCount | wait1 | wait2 | expected-count1 | expected-count2 |
      | "hello-dev" | 3             | 10    | 3     | 0               | 3               |


  @albDetatchSubnet
  Scenario Outline: AZ failure simulation by detaching a subnet
    Given ALB <elbName>
    When detach subnet <subnetId>
    Then wait for <wait1> minute
    And assertEC2 healthy host count = <expected-count1>
    And attach subnet <subnetId>
    Then wait for <wait2> minute
    And assertEC2 healthy host count = <expected-count2>

  @dev
    Examples:
      | elbName     | expected-count1 | expected-count2 | subnetId          | wait1 | wait2 |
      | "hello-dev" | 4               | 6               | "subnet-eda038b6" | 1     | 1     |
