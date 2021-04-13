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
  retry_strategy: function (options) {
    if (options.error && options.error.code === 'ECONNREFUSED') {
      // End reconnecting on a specific error and flush all commands with
      // a individual error
      return new Error('The server refused the connection');
    }
    if (options.total_retry_time > 1000 * 60 * 60) {
      // End reconnecting after a specific timeout and flush all commands
      // with a individual error
      return new Error('Retry time exhausted');
    }
    if (options.attempt > 10) {
      // End reconnecting with built in error
      return undefined;
    }
    // reconnect after
    return Math.min(options.attempt * 100, 3000);
  },
});

redisClient.on('connect', () => {
  console.log('Redis connected: redis.');
});

redisClient.on('error', function (err) {
  console.log('Redis error: ' + err);
});
