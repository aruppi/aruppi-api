import {
  Request,
  Response,
  NextFunction,
  ErrorRequestHandler,
  RequestHandler,
} from 'express';

/* 
  Error handler and notFound handler
  for all the API like a middleware with
  the function next of express.
*/

export const errorHandler: ErrorRequestHandler = (
  err: any,
  req: Request,
  res: Response,
  next: NextFunction,
) => {
  const statusCode = res.statusCode !== 200 ? res.statusCode : 500;
  res.status(statusCode).json({
    message: err.message,
    stack: process.env.NODE_ENV === 'production' ? '🥞' : err.stack,
  });
};

export const notFound: any = (
  req: Request,
  res: Response,
  next: NextFunction,
) => {
  res.status(404);
  const error = new Error(`🔍 - Not Found - ${req.originalUrl}`);
  next(error);
};

// export const requestLoggerMiddleWare: RequestHandler = (req, res, next) => {
//   console.log(`${req.method} ${req.originalUrl}`);
//   const start: number = new Date().getTime();
//   res.on('finish', () => {
//     const elapsed: number = new Date().getTime() - start;
//     console.info(
//       `${req.method} ${req.originalUrl} ${req.statusCode} ${elapsed}ms`,
//     );
//   });
//   next();
// };
