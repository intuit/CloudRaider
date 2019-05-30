@fmeaElastiCache


# This is an example scenario on how to use gherkin/cucumber based DSL to inject failures

Feature: ElastiCache Failures

  @rebootCache
  Scenario Outline: Reboot ElastiCache cluster
    Given ElastiCache <elastiCacheClusterName>
    When reboot ElastiCache nodes <numNodes>
    Then assertElastiCache cluster status <instantStatus1>
    And wait for <wait1> minute
    And assertElastiCache cluster status <instantStatus2>

    Examples:
      | elastiCacheClusterName | instantStatus1                  | wait1 | instantStatus2 | numNodes |
      | "hello-memcache-test"  | "rebooting cache cluster nodes" | 3     | "available"    | 2        |
      | "hello-memcache-test"  | "rebooting cache cluster nodes" | 3     | "available"    | 1        |


  @addCacheNode
  Scenario Outline: Add nodes to  ElastiCache cluster
    Given ElastiCache <elastiCacheClusterName>
    When add ElastiCache nodes <numNodes>
    Then assertElastiCache cluster status <instantStatus1>
    And wait for <wait1> minute
    And assertElastiCache cluster status <instantStatus2>

    Examples:
      | elastiCacheClusterName | instantStatus1 | wait1 | instantStatus2 | numNodes |
      | "hello-memcache-test"  | "modifying"    | 3     | "available"    | 1        |


  @removeCacheNodes
  Scenario Outline: Remove nodes from  ElastiCache cluster
    Given ElastiCache <elastiCacheClusterName>
    When remove ElastiCache nodes <numNodes>
    Then assertElastiCache cluster status <instantStatus1>
    And wait for <wait1> minute
    And assertElastiCache cluster status <instantStatus2>

    Examples:
      | elastiCacheClusterName | instantStatus1 | wait1 | instantStatus2 | numNodes |
      | "hello-memcache-test"  | "modifying"    | 3     | "available"    | 1        |


  @detachSubnet
  Scenario Outline: Detach subnet from ElastiCache SubnetGroup
    Given ElastiCache <elastiCacheClusterName>
    When detach ElastiCache subnet <subnetId>
    And wait for <wait1> minute
    And attach ElastiCache subnet <subnetId>


    Examples:
      | elastiCacheClusterName | wait1 | subnetId          |
      | "hellocache-a-dev"     | 1     | "subnet-d739c08d" |


  @changeSecurityGroup
  Scenario Outline: Change Security Group in ElastiCache to invoke app failure
    Given ElastiCache <elastiCacheClusterName>
    When change ElastiCache security group from <securityGroup> to <defaultSecurityGroup>
    Then assertElastiCache cluster status <instantStatus1>
    And wait for <wait1> minute
    Then assertElastiCache cluster status <instantStatus2>
    And change ElastiCache security group from <defaultSecurityGroup> to <securityGroup>
    And wait for <wait1> minute
    Then assertElastiCache cluster status <instantStatus2>



    Examples:
      | elastiCacheClusterName | instantStatus1 | instantStatus2 | wait1 | securityGroup | defaultSecurityGroup |
      | "hellocache-a-dev"     | "modifying"    | "available"    | 1     | "sg-2eaf4b51" | "sg-4ec49232"        |

