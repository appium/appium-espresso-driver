import executeCmds from './execute';
import generalCmds from './general';
import servicesCmds from './services';
import screenshotCmds from './screenshot';
import idlingResourcesCmds from './idling-resources';

const commands = {};
Object.assign(
  commands,
  generalCmds,
  executeCmds,
  servicesCmds,
  idlingResourcesCmds,
  screenshotCmds,
  // add other command types here
);

export default commands;
