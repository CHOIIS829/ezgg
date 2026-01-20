export const getRankImageSrc = (tier) => {
  const tierLower = (tier || "").toLowerCase();
  const validTiers = ["iron", "bronze", "silver", "gold", "platinum", "emerald", "diamond", "master", "grandmaster", "challenger"];

  return validTiers.includes(tierLower)
    ? `/ranks/${tierLower}.png`
    : "/ranks/unranked.png";
};

export const formatTierText = (tier, tierNum) => {
  if (!tier) return "Unranked";
  return tierNum ? `${tier} ${tierNum}` : tier;
};
