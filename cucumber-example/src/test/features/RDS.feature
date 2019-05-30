@fmeaFailover
@BlockDomain

# This is an example scenario on how to use gherkin/cucumber based DSL to inject failures

Feature: RDS Failures

  @rebootDBInstance
  Scenario Outline: Reboot a Db instance
    Given dBInstance <dbName>
    When reboot DbInstance
    Then assertRDS instance status <instantStatus1>
    And wait for <wait1> minute
    And assertRDS instance status <instantStatus2>

  @dev
    Examples:
      | dbName       | instantStatus1 | wait1 | instantStatus2 |
      | "HELLODVW2A" | "rebooting"    | 2     | "available"    |

  @detachDBInstance
  Scenario Outline: Detach a Db instance's security group and recover
    Given dBInstance <dbName>
    When detach DBSecurityGroup <secGroupId> with <defaultSecGroupId>
    Then assertRDS instance status <instantStatus2>
    And wait for <wait1> minute
    And attach DBSecurityGroup <secGroupId> with <defaultSecGroupId>
    And assertRDS instance status <instantStatus2>

  @dev
    Examples:
      | dbName       | secGroupId    | instantStatus1 | instantStatus2 | wait1 | defaultSecGroupId |
      | "HELLOEEW2B" | "sg-e125c59d" | "modifying"    | "available"    | 5     | "sg-8e491ef3"     |


  @detachSubnet
  Scenario Outline: Detach subnet a Db instance
    Given dBInstance <dbName>
    When detach DB subnet <subnetId>
    Then assertRDS instance status <instantStatus2>
    And wait for <wait1> minute
    And attach DB subnet <subnetId>
    And assertRDS instance status <instantStatus2>

  @dev
    Examples:
      | dbName       | instantStatus1 | instantStatus2 | wait1 | subnetId          |
      | "HELLODVW2A" | "modifying"    | "available"    | 2     | "subnet-1a183452" |


  @snapshotDBInstance
  Scenario Outline: RDS Snapshot
    Given dBInstance <dbName>
    When create DB snapshot with name <snapshotName>
    Then wait for <wait1> minute
    And assertRDS instance status <instantStatus1>
    And wait for <wait2> minute
    And assertRDS instance status <instantStatus2>

  @dev
    Examples:
      | dbName       | instantStatus1 | instantStatus2 | wait1 | wait2 | snapshotName |
      | "HELLODVW2A" | "backing-up"   | "available"    | 1     | 2     | "test3"      |

  @restoreDBInstance
  Scenario Outline: RDS Restore from Snapshot
    Given dBInstance <dbName>
    When restore DB from snapshot with name <snapshotName>
    Then wait for <wait1> minute
    And assertRDS instance status <instantStatus1>
    And wait for <wait2> minute
    And assertRDS instance status <instantStatus2>

  @dev
    Examples:
      | dbName       | instantStatus1 | instantStatus2 | wait1 | wait2 | snapshotName |
      | "HELLODVW2A" | "backing-up"   | "available"    | 1     | 2     | "test3"      |


  @dbInstanceStorageUpdate
  Scenario Outline: RDS storage update
    Given dBInstance <dbName>
    When change DB storage size to <storageSize1> GB
    And assertRDS instance status <instantStatus1>
    And wait for <wait1> minute
    And assertRDS instance status <instantStatus2>

  @dev
    Examples:
      | dbName       | instantStatus1 | instantStatus2         | wait1 | storageSize1 |
      | "HELLODVW2A" | "modifying"    | "storage-optimization" | 3     | 101          |


  @dbInstanceModifyInstanceClass
  Scenario Outline: RDS modify instance class
    Given dBInstance <dbName>
    When change DB InstanceClass <instanceClass1>
    And wait for <wait1> minute
    And assertRDS instance status <instantStatus1>
    And wait for <wait2> minute
    And assertRDS instance status <instantStatus2>
    And change DB InstanceClass <instanceClass2>
    And wait for <wait2> minute
    And assertRDS instance status <instantStatus2>

  @dev
    Examples:
      | dbName       | instantStatus1 | instantStatus2 | wait1 | wait2 | instanceClass1 | instanceClass2 |
      | "HELLODVW2A" | "modifying"    | "available"    | 1     | 25    | "db.t2.small"  | "db.t2.medium" |