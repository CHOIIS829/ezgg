import fs from 'fs';
import path from 'path';
import https from 'https';
import { champions } from '../src/data/champions.js';

const DDRAGON_VERSION = '14.5.1';
const PUBLIC_DIR = path.join(process.cwd(), 'public');
const CHAMPION_IMAGES_DIR = path.join(PUBLIC_DIR, 'champions');
const RANK_IMAGES_DIR = path.join(PUBLIC_DIR, 'ranks');

// Create directories if they don't exist
if (!fs.existsSync(PUBLIC_DIR)) {
  fs.mkdirSync(PUBLIC_DIR);
}
if (!fs.existsSync(CHAMPION_IMAGES_DIR)) {
  fs.mkdirSync(CHAMPION_IMAGES_DIR);
}
if (!fs.existsSync(RANK_IMAGES_DIR)) {
  fs.mkdirSync(RANK_IMAGES_DIR);
}

// Download champion images
champions.forEach(champion => {
  const url = `https://ddragon.leagueoflegends.com/cdn/${DDRAGON_VERSION}/img/champion/${champion.id}.png`;
  const filePath = path.join(CHAMPION_IMAGES_DIR, champion.image);
  
  if (!fs.existsSync(filePath)) {
    https.get(url, (response) => {
      const fileStream = fs.createWriteStream(filePath);
      response.pipe(fileStream);
      
      fileStream.on('finish', () => {
        console.log(`Downloaded: ${champion.name}`);
        fileStream.close();
      });
    }).on('error', (err) => {
      console.error(`Error downloading ${champion.name}:`, err.message);
    });
  }
});

// Download rank emblems
const ranks = [
  { name: 'Iron', file: 'Iron.png' },
  { name: 'Bronze', file: 'Bronze.png' },
  { name: 'Silver', file: 'Silver.png' },
  { name: 'Gold', file: 'Gold.png' },
  { name: 'Platinum', file: 'Platinum.png' },
  { name: 'Emerald', file: 'Emerald.png' },
  { name: 'Diamond', file: 'Diamond.png' },
  { name: 'Master', file: 'Master.png' },
  { name: 'Grandmaster', file: 'Grandmaster.png' },
  { name: 'Challenger', file: 'Challenger.png' }
];

ranks.forEach(rank => {
  const url = `https://raw.githubusercontent.com/esports-bits/lol_images/master/ranks/${rank.name}.png`;
  const filePath = path.join(RANK_IMAGES_DIR, rank.file);
  
  if (!fs.existsSync(filePath)) {
    https.get(url, (response) => {
      const fileStream = fs.createWriteStream(filePath);
      response.pipe(fileStream);
      
      fileStream.on('finish', () => {
        console.log(`Downloaded: ${rank.name} rank emblem`);
        fileStream.close();
      });
    }).on('error', (err) => {
      console.error(`Error downloading ${rank.name} rank emblem:`, err.message);
    });
  }
}); 