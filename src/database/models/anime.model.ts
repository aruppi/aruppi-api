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
  state: string;
  score: string;
  jkanime: boolean;
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
  state: { type: String },
  score: { type: String },
  jkanime: { type: Boolean },
  description: { type: String },
});

export default model<Anime>('Anime', AnimeSchema);