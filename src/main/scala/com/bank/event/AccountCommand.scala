package com.bank.event

import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, SaveSnapshotFailure, SaveSnapshotSuccess, SnapshotOffer}

// Commands for the Bank Account
sealed trait AccountCommand
case class Deposit(amount: Double) extends AccountCommand
case class Withdraw(amount: Double) extends AccountCommand
case object GetBalance extends AccountCommand

// Persistent Actor for Bank Account
class BankAccount extends PersistentActor with ActorLogging {

  // Persistence Identifier (unique for each actor)
  override def persistenceId: String = "bank-account"

  // Internal state: balance
  var balance: Double = 0.0

  // Handle incoming commands
  override def receiveCommand: Receive = {
    case Deposit(amount) =>
      log.info(s"Depositing $amount")
      persist(Deposited(amount)) { event =>
        updateState(event)
        log.info(s"New balance: $balance")
        takeSnapshot()
      }

    case Withdraw(amount) =>
      if (amount > balance) {
        log.warning(s"Cannot withdraw $amount, insufficient funds")
      } else {
        log.info(s"Withdrawing $amount")
        persist(Withdrawn(amount)) { event =>
          updateState(event)
          log.info(s"New balance: $balance")
          takeSnapshot()
        }
      }

    case GetBalance =>
      log.info(s"Current balance: $balance")

    case SaveSnapshotSuccess(metadata) =>
      log.info(s"Snapshot saved successfully: $metadata")

    case SaveSnapshotFailure(metadata, reason) =>
      log.warning(s"Snapshot failed: $metadata, reason: $reason")
  }

  // Handle replaying events (recovery)
  override def receiveRecover: Receive = {
    case event: AccountEvent =>
      updateState(event)

    case SnapshotOffer(_, snapshot: Double) =>
      log.info(s"Restoring from snapshot: $snapshot")
      balance = snapshot
  }

  // Update the actor's state based on events
  def updateState(event: AccountEvent): Unit = event match {
    case Deposited(amount) => balance += amount
    case Withdrawn(amount) => balance -= amount
  }

  // Optionally, take a snapshot of the actor's state
  def takeSnapshot(): Unit = {
    if (Math.abs(balance) > 100) { // You can customize the snapshot conditions
      log.info(s"Taking snapshot at balance: $balance")
      saveSnapshot(balance)
    }
  }
}

