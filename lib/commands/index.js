import executeCmds from './execute';
import generalCmds from './general';

let commands = {};
Object.assign(
  commands,
  generalCmds,
  executeCmds,
  // add other command types here
);

export default commands;
