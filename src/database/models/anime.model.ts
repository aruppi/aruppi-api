import { Document, model, Types, Schema } from 'mongoose';

/*
  This is the model for each anime
  of the directory, the anime model.
*/

export interface Anime extends Document {
  id: string;
  title: string;
  mal_id: number;
  poster: string;
  type: string;
  genres: Types.Array<string>;
  score: string;
  source: string;
  description: string;
}

// Schema for the anime
const AnimeSchema: Schema = new Schema({
  id: { type: String },
  title: { type: String },
  mal_id: { type: Number },
  poster: { type: String },
  type: { type: String },
  genres: [{ type: String }],
  score: { type: String },
  source: { type: String },
  description: { type: String },
});

export default model<Anime>('Anime', AnimeSchema);
