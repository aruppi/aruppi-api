import { Document, model, Types, Schema } from 'mongoose';

/*
  This is the model for each genre
  of the directory, the genre model.
*/

export interface Genre extends Document {
  name: string;
  value: string;
}

// Schema for the theme
const GenreSchema: Schema = new Schema({
  name: { type: String },
  value: { type: String },
});

export default model<Genre>('Genre', GenreSchema);
