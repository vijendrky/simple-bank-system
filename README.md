Example: A Simple Bank Account Actor Using Akka Persistence
Let’s build an example where we use Akka Persistence to model a simple bank account. The account supports depositing money, withdrawing money, and querying the balance. All account transactions (deposits and withdrawals) will be persisted as events.

Step 1: Add Dependencies
Make sure you have the following dependencies in your build.sbt file:

sbt
Copy code
libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "2.6.20"
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-query" % "2.6.20"
You also need to configure an event journal and snapshot store. For this example, we will use the in-memory journal (for demonstration purposes). You can configure more durable storage options, like Cassandra or PostgreSQL, for production use.

In your application.conf file, configure the persistence:

hocon
Copy code
akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
Step 2: Define the Events
Define the events that represent changes in the bank account (e.g., deposits and withdrawals).

scala
Copy code
sealed trait AccountEvent
case class Deposited(amount: Double) extends AccountEvent
case class Withdrawn(amount: Double) extends AccountEvent
These events represent changes that occur to the actor's state (balance) and will be persisted.

Step 3: Define the Bank Account Persistent Actor
Now, we define a persistent actor that models a simple bank account.

scala
Copy code
import akka.actor.ActorLogging
import akka.persistence.{PersistentActor, SnapshotOffer}

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
Explanation of the Persistent Actor:
persistenceId:

Each persistent actor must define a unique persistenceId that identifies it in the journal and snapshot store.
receiveCommand:

This method handles incoming commands. For each command, we persist the event using the persist method, which ensures the event is written to the journal.
After persisting, we apply the event by calling updateState.
receiveRecover:

This method is used to recover the actor’s state during startup. It replays events from the journal and restores snapshots.
The SnapshotOffer is used when a snapshot is available, which avoids replaying all events.
updateState:

This method applies an event to update the actor’s state. It’s called during both normal operation and recovery.
takeSnapshot:

This is a helper method to periodically take snapshots of the actor’s state. In this case, a snapshot is taken when the balance crosses 100.
Step 4: Create and Run the Actor
Now, let’s create an actor system and interact with the BankAccount actor.

scala
Copy code
import akka.actor.{ActorSystem, Props}

object BankAccountApp extends App {
val system = ActorSystem("BankAccountSystem")

val bankAccount = system.actorOf(Props[BankAccount], "bankAccountActor")

// Send commands to the actor
bankAccount ! Deposit(150.0)
bankAccount ! Withdraw(50.0)
bankAccount ! GetBalance

// Terminate the system after some time
Thread.sleep(2000)
system.terminate()
}
Output:
csharp
Copy code
[INFO] Depositing 150.0
[INFO] New balance: 150.0
[INFO] Taking snapshot at balance: 150.0
[INFO] Snapshot saved successfully: SnapshotMetadata(bank-account,0,1662296592297)
[INFO] Withdrawing 50.0
[INFO] New balance: 100.0
[INFO] Current balance: 100.0
Step 5: Recovery Example
If the actor crashes or restarts, Akka Persistence will replay the stored events or restore the snapshot to recover the actor's state.

If you restart the BankAccount actor and send the GetBalance command again, the actor will recover from the snapshot and replay any missing events.