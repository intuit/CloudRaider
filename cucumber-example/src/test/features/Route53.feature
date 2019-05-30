@fmeaFailover
@R53Failure


# This is an example scenario on how to use gherkin/cucumber based DSL to inject failures

Feature: Route53 Failover

  @route53Failover
  Scenario Outline: Route53 failover by bringing down hosts in primary region
    Given ALB <albName>
    And CloudWatch Alarm <alarmName>
    And R53 Healthcheck ID <healthCheckId>
    When terminate all instances
    Then wait for <wait1> minute
    And assertCW alarm = <state1>
    And assertTrue R53 failover from <primary> to <secondary>
    And assertR53 HealthCheck state = <healthCheckState1>
    And wait for <wait2> minute
    And assertCW alarm = <state2>
    And assertFalse R53 failover from <primary> to <secondary>
    And assertR53 HealthCheck state = <healthCheckState2>

  @dev
    Examples:
      | albName     | alarmName                                      | primary           | secondary                   | wait1 | wait2 | state1  | state2 | healthCheckId                         | healthCheckState1 | healthCheckState2 |
      | "hello-dev" | "elb-hello-ihp-prf-ElbUnhealthyHostCountAlarm" | "hello.world.com" | "(([^/])+)?helloworld.com." | 3     | 5     | "ALARM" | "OK"   | 1a6d5cce-6c4e-4949-bb70-3af6fb716d4d" | "FAILURE"         | "SUCCESS"         |


  @detachALBSubnet
  Scenario Outline: hello ELB failure by detaching a Security Group
    Given ALB <albName>
    When detach security-group <securityGroupId>
    Then wait for <wait1> minute
    And assertEC2 healthy host count = <expected-count1>
    And assertR53 HealthCheckId <healthCheckFull> with state = <healthCheckState1>
    And assertR53 HealthCheckId <healthCheckUnhealthyHost> with state = <healthCheckState2>
    And assertTrue R53 failover from <primary> to <secondary>
    And attach security-group <securityGroupId>
    Then wait for <wait2> minute
    And assertEC2 healthy host count = <expected-count2>
    And assertR53 HealthCheckId <healthCheckFull> with state = <healthCheckState2>
    And assertR53 HealthCheckId <healthCheckUnhealthyHost> with state = <healthCheckState2>
    And assertFalse R53 failover from <primary> to <secondary>

    Examples:
      | albName     | expected-count1 | expected-count2 | securityGroupId | wait1 | wait2 | healthCheckFull                        | healthCheckUnhealthyHost               | healthCheckState1 | healthCheckState2 | primary           | secondary                   |
      | "hello-dev" | 6               | 6               | "sg-7c5e5706"   | 2     | 4     | "1a6d5cce-6c4e-4949-bb70-3af6fb716d4d" | "bef3fd49-462f-48e2-934b-f4bdf243748b" | "FAILURE"         | "SUCCESS"         | "hello.world.com" | "(([^/])+)?helloworld.com." |


  @route53TrafficPolicyUpdate
  Scenario Outline: hello Route53 update traffic policy
    Given R53 traffic policy name <route53PolicyName>
    When R53 update traffic policy version to <versionNumber>
    Then assertR53 traffic policy versionId = <versionNumber>

  @PRF
    Examples:
      | route53PolicyName | versionNumber |
      | "Hello-Policy"    | 6             |


  @route53HealthCheck
  Scenario Outline: Route53 update health check regions
    Given R53 Healthcheck ID <healthCheckId>
    And R53 update HealthCheck regions to <regionsList>
    And assertR53 HealthCheck regions = <regionsList>
    And wait for <wait1> minute
    And R53 reset HealthCheck to default regions
    And assertR53 HealthCheck regions = <defaultRegionsList>

  @dev
    Examples:
      | healthCheckId                          | regionsList                     | defaultRegionsList                                                                                      | wait1 |
      | "1a6d5cce-6c4e-4949-bb70-3af6fb716d4d" | "us-west-2,us-west-1,us-east-1" | "sa-east-1, us-west-1, us-west-2, ap-northeast-1, ap-southeast-1, eu-west-1, us-east-1, ap-southeast-2" | 2     |
