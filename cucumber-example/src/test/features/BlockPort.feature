@fmeaFailure
@BlockPort

# This is an example scenario on how to use gherkin/cucumber based DSL to inject failures

Feature: Amazon EC2 instance block port

  @ssmBlockPort
  Scenario Outline: Blocking port on ec2 hosts via AWS Simple System Manager (SSM)

    Given ALB <albName>
    When SSM block network port <portNum> on <instanceCount> instances
    Then wait for <wait1> minute
    And assertCommand execution status = <commandStatus>
    And assertEC2 healthy host count = <expected-count1>
    And recover
    And wait for <wait2> minute
    And assertEC2 healthy host count = <expected-count2>

  @dev
    Examples:
      | albName     | portNum | instanceCount | wait1 | wait2 | expected-count1 | expected-count2 | commandStatus |
      | "hello-dev" | 8080    | 3             | 5     | 1     | 0               | 1               | "Success"     |
      | "hello-dev" | 80      | 3             | 5     | 1     | 0               | 1               | "Success"     |

