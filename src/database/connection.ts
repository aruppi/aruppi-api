import mongoose from 'mongoose';
import redis, { RedisClient } from 'redis';
import dotenv from 'dotenv';

// Configuring dotenv to read the variable from .env file
dotenv.config();

/* 
  Create the connection to the database
  of mongodb.
*/

export const createConnectionMongo = (databaseObj: {
  port: string | undefined;
  host: string | undefined;
}) => {
  mongoose.connect(
    `mongodb://${databaseObj.host}:${databaseObj.port}/anime-directory`,
    {
      useNewUrlParser: true,
      useUnifiedTopology: true,
    },
  );

  mongoose.connection.on('error', err => {
    console.log('err', err);
  });
  mongoose.connection.on('connected', (err, res) => {
    console.log('Database connected: mongoose.');
  });
};

/* 
  Create the connection to the cache of
  redis, and exporting the redis client
  with the call of this file.
*/

export const redisClient: RedisClient = redis.createClient({
  host: process.env.REDIS_HOST,
  port: parseInt(process.env.REDIS_PORT!),
  password: process.env.REDIS_PASSWORD,
});

redisClient.on('connect', () => {
  console.log('Redis connected: redis.');
});
