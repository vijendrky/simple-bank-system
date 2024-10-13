package com.bank.event

sealed trait AccountEvent
case class Deposited(amount: Double) extends AccountEvent
case class Withdrawn(amount: Double) extends AccountEvent
