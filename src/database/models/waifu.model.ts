import { Document, model, Schema } from 'mongoose';

/*
  This is the model for each anime
  of the directory, the anime model.
*/

export interface Waifu extends Document {
  id: string;
  name: string;
  weight: string;
  series: object;
  height: string;
  birthday: string;
  likes: number;
  trash: number;
  blood_type: string;
  hip: string;
  bust: string;
  description: string;
  display_picture: string;
  waist: string;
}

// Schema for the Waifu
const WaifuSchema: Schema = new Schema({
  id: { type: String },
  name: { type: String },
  weight: { type: String },
  series: { type: Object },
  height: { type: String },
  birthday: { type: String },
  likes: { type: Number },
  trash: { type: Number },
  blood_type: { type: String },
  hip: { type: String },
  bust: { type: String },
  description: { type: String },
  display_picture: { type: String },
  waist: { type: String },
});

export default model<Waifu>('Waifu', WaifuSchema);
