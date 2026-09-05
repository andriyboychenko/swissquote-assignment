function parsePolicyReference(sourceReference) {
  const match = /^policy:\/\/policies\/([^#]+)#(.+)$/.exec(sourceReference);
  if (!match) {
    throw new Error("Unsupported policy reference");
  }

  return {
    documentName: match[1],
    sectionAnchor: match[2]
  };
}

export async function fetchPolicySection(sourceReference) {
  const { documentName, sectionAnchor } = parsePolicyReference(sourceReference);
  const response = await fetch(
    `/api/policies/${encodeURIComponent(documentName)}/sections/${encodeURIComponent(sectionAnchor)}`
  );

  if (!response.ok) {
    throw new Error("Could not load policy section");
  }

  return response.json();
}
