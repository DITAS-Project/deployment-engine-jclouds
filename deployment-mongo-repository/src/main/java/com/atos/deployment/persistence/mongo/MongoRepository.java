package com.atos.deployment.persistence.mongo;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

public class MongoRepository {

  protected MongoClient client;

  public MongoRepository() {
    client = createMongoClient();
  }

  protected MongoClient createMongoClient() {
    CodecRegistry pojoCodecRegistry =
        CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()));
    return MongoClients.create(
        MongoClientSettings.builder().codecRegistry(pojoCodecRegistry).build());
  }
}
