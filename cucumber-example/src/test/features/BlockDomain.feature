@fmeaFailure
@BlockDomain

# This is an example scenario on how to use gherkin/cucumber based DSL to inject failures

Feature: Amazon EC2 instance block domain/dependency

  @ssmBlockDomain
  Scenario Outline: Blocking dependency on ec2 hosts via AWS Simple System Manager (SSM)

    Given ALB <albName>
    When SSM block domain <domainName> on <instanceCount> instances
    Then wait for <wait1> minute
    And assertCommand execution status = <commandStatus>
    And wait for <wait2> minute
    And assertEC2 healthy host count = <expected-count1>
    And recover
    And wait for <wait2> minute
    And assertEC2 healthy host count = <expected-count2>

  @dev
    Examples:
      | albName     | domainName     | instanceCount | wait1 | wait2 | expected-count1 | commandStatus | expected-count2 |
      | "hello-dev" | "mydomain.com" | 3             | 1     | 4     | 3               | "Success"     | 3               |


  @sshBlockDomain
  Scenario Outline: Blocking dependency on ec2 hosts via SSH (See ReadMe document for setup)

    Given ALB <albName>
    When block domain <domainName> on <instanceCount> instances
    Then wait for <wait1> minute
    And assertCommand execution status = <commandStatus>
    And wait for <wait2> minute
    And assertEC2 healthy host count = <expected-count1>
    And recover
    And wait for <wait2> minute
    And assertEC2 healthy host count = <expected-count2>

  @dev
    Examples:
      | albName     | domainName     | instanceCount | wait1 | wait2 | expected-count1 | commandStatus | expected-count2 |
      | "hello-dev" | "mydomain.com" | 3             | 1     | 4     | 3               | "Success"     | 3               |


  @ssmBlockCIDR
  Scenario Outline: Blocking CIDR on ec2 hosts via AWS Simple System Manager (SSM)

    Given ALB <albName>
    When SSM block domain <domainName> on <instanceCount> instances
    Then wait for <wait1> minute
    And assertCommand execution status = <commandStatus>
    And wait for <wait2> minute
    And assertEC2 healthy host count = <expected-count1>
    And recover
    And wait for <wait2> minute
    And assertEC2 healthy host count = <expected-count2>

  @dev
    Examples:
      | albName     | domainName       | instanceCount | wait1 | wait2 | expected-count1 | commandStatus | expected-count2 |
      | "hello-dev" | "10.85.238.0/24" | 3             | 1     | 4     | 3               | "Success"     | 3               |