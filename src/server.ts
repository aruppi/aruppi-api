import express, { Application } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import dotenv from 'dotenv';
import { errorHandler, notFound } from './middlewares/middleware';
import { createConnection } from './database/connection';
import routes from './routes';

const app: Application = express();

dotenv.config();
createConnection(process.env.DATABASE);
app.use(cors());
app.use(helmet());
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(routes);
app.use(notFound);
app.use(errorHandler);

/*
  Starting the server on the .env process
  you can define the PORT where the server
  is going to listen in the server.
  ex: PORT=3000.
*/
app.listen(process.env.PORT || 3000);
