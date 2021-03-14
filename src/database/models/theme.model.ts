import { Document, model, Types, Schema } from 'mongoose';

/*
  This is the model for each theme
  of the directory, the theme model.
*/

interface TInterface {
  title: string;
  video: string;
  type: string;
}

export interface Theme extends Document {
  id: string;
  title: string;
  year: string;
  themes: Types.Array<TInterface>;
}

// Schema for the theme
const ThemeSchema: Schema = new Schema({
  id: { type: String },
  title: { type: String },
  year: { type: String },
  themes: [{ type: Object }],
});

export default model<Theme>('Theme', ThemeSchema);
