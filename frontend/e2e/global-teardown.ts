import {readFileSync} from 'node:fs';
import {resolve} from 'node:path';
import {Client} from 'pg';

/** Doit rester aligné sur newTestAccount() (helpers.ts) : ne cible que les emails générés par les tests. */
const TEST_ACCOUNT_EMAIL = '^e2e-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}@test\\.com$';

/**
 * Supprime les comptes créés par les tests, une fois tous les tests terminés.
 * La connexion à la base est lue dans la configuration du backend.
 */
export default async function globalTeardown(): Promise<void> {
  const properties = readFileSync(
    resolve(__dirname, '../../backend/src/main/resources/application.properties'),
    'utf8',
  );
  const property = (name: string): string => {
    const line = properties.split(/\r?\n/).find(l => l.startsWith(`${name}=`));
    if (line === undefined) {
      throw new Error(`Propriété ${name} introuvable dans application.properties`);
    }
    return line.slice(name.length + 1).trim();
  };

  const url = new URL(property('spring.datasource.url').replace(/^jdbc:/, ''));
  if (url.hostname !== 'localhost') {
    throw new Error(`Nettoyage e2e refusé : la base n'est pas locale (${url.hostname})`);
  }

  const client = new Client({
    host: url.hostname,
    port: Number(url.port),
    database: url.pathname.slice(1),
    user: property('spring.datasource.username'),
    password: property('spring.datasource.password'),
  });

  await client.connect();
  try {
    await client.query('DELETE FROM users WHERE email ~ $1', [TEST_ACCOUNT_EMAIL]);
  } finally {
    await client.end();
  }
}
