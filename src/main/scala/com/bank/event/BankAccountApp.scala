package com.bank.event

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
