package me.linhvo.ittakestwo.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import me.linhvo.ittakestwo.Configs
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class ConfigsSerializer @Inject constructor() : Serializer<Configs> {
    override val defaultValue: Configs = Configs.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Configs {
        try {
            return Configs.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: Configs, output: OutputStream) {
        return t.writeTo(output)
    }
}