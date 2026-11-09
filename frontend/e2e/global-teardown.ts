import {connectToDatabase} from './db';

/** Doit rester aligné sur newTestAccount() (helpers.ts) : ne cible que les emails générés par les tests. */
const TEST_ACCOUNT_EMAIL = '^e2e-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}@test\\.com$';

/**
 * Supprime, une fois tous les tests terminés :
 * - les comptes créés par les tests (email e2e-…@test.com) ;
 * - les invités créés pendant les tests (id plus grand que celui noté par global-setup).
 * Leurs listes et les entrées de ces listes partent avec eux.
 */
export default async function globalTeardown(): Promise<void> {
  const maxUserIdBeforeTests = process.env['E2E_MAX_USER_ID_BEFORE_TESTS'];
  // Sans la valeur de global-setup, on ne touche à aucun invité plutôt que de risquer de supprimer les tiens.
  const testUsers = maxUserIdBeforeTests === undefined
    ? {where: 'email ~ $1', params: [TEST_ACCOUNT_EMAIL]}
    : {where: 'email ~ $1 OR (guest AND id > $2)', params: [TEST_ACCOUNT_EMAIL, maxUserIdBeforeTests]};
  const testUserIds = `SELECT id FROM users WHERE ${testUsers.where}`;

  const client = await connectToDatabase();
  try {
    // Les listes pointent vers leur compte (clé étrangère user_id), et leurs entrées vers la liste :
    // on supprime donc des feuilles vers la racine, dans une transaction pour ne rien laisser à moitié.
    await client.query('BEGIN');
    await client.query(
      `DELETE FROM games_list_entry WHERE games_list_id IN (SELECT id FROM games_list WHERE user_id IN (${testUserIds}))`,
      testUsers.params,
    );
    await client.query(`DELETE FROM games_list WHERE user_id IN (${testUserIds})`, testUsers.params);
    await client.query(`DELETE FROM users WHERE ${testUsers.where}`, testUsers.params);
    await client.query('COMMIT');
  } catch (error) {
    await client.query('ROLLBACK');
    throw error;
  } finally {
    await client.end();
  }
}
