import mongoose from 'mongoose';
// import redis, { RedisClient } from 'redis';

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

// export const createConnectionRedis = (redisObj: {
//   host: string;
//   port: number;
// }) => {
//   const client: RedisClient = redis.createClient({
//     host: redisObj.host,
//     port: redisObj.port,
//   });

//   client.on('connect', () => {
//     console.log('Redis connected: redis.');
//   });
// };
