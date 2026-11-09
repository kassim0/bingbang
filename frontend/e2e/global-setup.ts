import {connectToDatabase} from './db';

/**
 * Chaque page ouverte sans token crée un compte invité, impossible à distinguer par son email
 * d'un invité créé à la main pendant le développement. On note donc le plus grand id de compte
 * avant les tests : global-teardown ne supprimera que les invités créés après.
 */
export default async function globalSetup(): Promise<void> {
  const client = await connectToDatabase();
  try {
    const result = await client.query('SELECT COALESCE(MAX(id), 0) AS max_id FROM users');
    process.env['E2E_MAX_USER_ID_BEFORE_TESTS'] = String(result.rows[0].max_id);
  } finally {
    await client.end();
  }
}
