import express, { Application } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import dotenv from 'dotenv';
import { errorHandler, notFound } from './middlewares/middleware';
import routes from './routes';

const app: Application = express();

dotenv.config();
app.use(cors());
app.use(helmet());
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(routes);
app.use(errorHandler);
app.use(notFound);

// Starting to listen the server in port
app.listen(process.env.PORT || 3000);
