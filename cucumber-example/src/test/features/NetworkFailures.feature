@fmeaFailure
@NetworkFailure

# This is an example scenario on how to use gherkin/cucumber based DSL to inject failures

Feature: Amazon EC2 instance network related failures


  @SSMNetworkCorrupt
  Scenario Outline: Packet corruption on EC2 instances
    Given ALB <albName>
    When SSM corrupt network <percentCorrupt> percent on <instanceCount> instances
    Then wait for <wait1> minute
    And assertEC2 healthy host count = <expected-count1>
    # This will auto-heal as  network corruption will make host unavailable (depending on percent packet corruption
    And recover
    And wait for <wait2> minute
    And assertEC2 healthy host count = <expected-count2>

  @dev
    Examples:
      | albName     | percentCorrupt | instanceCount | wait1 | wait2 | expected-count1 | expected-count2 |
      | "hello-dev" | 98             | 1             | 4     | 4     | 0               | 1               |


  @SSMNetworkLatency
  Scenario Outline: Network Latency on EC2 Instances via SSM
    Given ALB <albName>
    When SSM inject network latency <minTime> ms to <maxTime> ms on <instanceCount> instances
    Then wait for <wait1> minute
    And assertCommand execution status = <commandStatus>
    And assertEC2 healthy host count = <expected-count1>
    And recover
    And wait for <wait2> minute
    And assertEC2 healthy host count = <expected-count2>

  @dev
    Examples:
      | albName     | instanceCount | wait1 | wait2 | expected-count1 | expected-count2 | minTime | maxTime | commandStatus |
      | "hello-dev" | 3             | 5     | 3     | 3               | 3               | 500     | 2000    | "Success"     |


  @SSMNetworkLatency
  Scenario Outline: Network Latency on EC2 Instances via SSH
    Given ALB <albName>
    When inject network latency <minTime> ms to <maxTime> ms on <instanceCount> instances
    Then wait for <wait1> minute
    And assertCommand execution status = <commandStatus>
    And assertEC2 healthy host count = <expected-count1>
    And recover
    And wait for <wait2> minute
    And assertEC2 healthy host count = <expected-count2>

  @dev
    Examples:
      | albName     | instanceCount | wait1 | wait2 | expected-count1 | expected-count2 | minTime | maxTime | commandStatus |
      | "hello-dev" | 3             | 5     | 3     | 3               | 3               | 500     | 2000    | "Success"     |

