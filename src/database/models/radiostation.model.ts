import { Document, model, Schema } from 'mongoose';

/*
  This is the model for each radiostation
  of the directory, the radiostation model.
*/

export interface RadioStation extends Document {
  name: string;
  url: string;
}

// Schema for the theme
const RadioStationSchema: Schema = new Schema({
  name: { type: String },
  url: { type: String },
});

export default model<RadioStation>('RadioStation', RadioStationSchema);
