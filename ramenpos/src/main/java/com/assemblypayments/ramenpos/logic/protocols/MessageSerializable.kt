package com.assemblypayments.ramenpos.logic.protocols

import com.assemblypayments.spi.model.Message
import java.io.Serializable

class MessageSerializable : Serializable {
    var message: Message = Message()
}