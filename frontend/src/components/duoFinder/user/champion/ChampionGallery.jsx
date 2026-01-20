import styled from '@emotion/styled';
import {ChampionPortrait} from './ChampionPortrait.jsx';

export const ChampionGallery = ({ champions }) => (
  <Gallery>
    <GalleryInner>
      {
        champions && Object.keys(champions).length > 0 ? (
          Object.values(champions).map((champion) => (
          <ChampionPortrait key={champion.championName} champion={champion.championName} />
        ))
      ) : (
        <>
          <ChampionPortrait champion="basic_1" />
          <ChampionPortrait champion="basic_2" />
          <ChampionPortrait champion="basic_3" />
        </>
      )}
    </GalleryInner>
  </Gallery>
);

const Gallery = styled.div`
  display: flex;
  height: 200px;
  flex-shrink: 0;
  width: 100%;
  justify-content: center;
  align-items: center;
`;

const GalleryInner = styled.div`
  display: flex;
  gap: 2px;
  & > * {
    width: 150px;
    height: 200px;
  }
`;
