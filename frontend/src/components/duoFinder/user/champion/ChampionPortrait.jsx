import { getChampionImageSrc } from '../../../../utils/championUtils.js';

export const ChampionPortrait = ({ champion }) => (
  <img
    src={getChampionImageSrc(champion)}
    alt={champion || "Unknown Champion"}
    onError={(e) => {
      e.target.src = '/champions/Default.png';
    }}
  />
);
