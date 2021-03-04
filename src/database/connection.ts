import mongoose from 'mongoose';

/* 
  Create the connection to the database
  of mongodb.
*/

export const createConnection = (database: string | undefined) => {
  mongoose.connect(`mongodb://${database}/anime-directory`, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
  });

  mongoose.connection.on('error', err => {
    console.log('err', err);
  });
  mongoose.connection.on('connected', (err, res) => {
    console.log('Database connected: mongoose.');
  });
};
