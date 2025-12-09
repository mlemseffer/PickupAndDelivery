import React from 'react';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faArrowDown,
  faArrowRight,
  faArrowUp,
  faArrowRotateRight,
  faBox,
  faBullseye,
  faChartBar,
  faCheck,
  faClipboardList,
  faCircleCheck,
  faCircleInfo,
  faCircleXmark,
  faFileUpload,
  faHashtag,
  faIndustry,
  faLightbulb,
  faLocationDot,
  faMap,
  faMagnifyingGlass,
  faPen,
  faPersonBiking,
  faPlus,
  faRoad,
  faRocket,
  faRuler,
  faStopwatch,
  faTrash,
  faTriangleExclamation,
  faUsers,
  faWarehouse,
  faWandMagicSparkles,
} from '@fortawesome/free-solid-svg-icons';

const iconMap = {
  warning: faTriangleExclamation,
  success: faCircleCheck,
  error: faCircleXmark,
  info: faCircleInfo,
  rocket: faRocket,
  box: faBox,
  location: faLocationDot,
  road: faRoad,
  ruler: faRuler,
  users: faUsers,
  search: faMagnifyingGlass,
  lightbulb: faLightbulb,
  bike: faPersonBiking,
  sparkles: faWandMagicSparkles,
  map: faMap,
  upload: faFileUpload,
  clipboard: faClipboardList,
  trash: faTrash,
  plus: faPlus,
  pen: faPen,
  rotate: faArrowRotateRight,
  arrowRight: faArrowRight,
  bullseye: faBullseye,
  warehouse: faWarehouse,
  factory: faIndustry,
  chart: faChartBar,
  timer: faStopwatch,
  arrowUp: faArrowUp,
  arrowDown: faArrowDown,
  check: faCheck,
  number: faHashtag,
};

export function Icon({ name, className = '', ...props }) {
  const icon = iconMap[name];
  if (!icon) return null;
  return <FontAwesomeIcon icon={icon} className={className} {...props} />;
}

export default Icon;

