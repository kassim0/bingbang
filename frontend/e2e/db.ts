import {readFileSync} from 'node:fs';
import {resolve} from 'node:path';
import {Client} from 'pg';

/**
 * Connexion à la base locale du backend, lue dans sa configuration.
 * Utilisée par global-setup et global-teardown pour nettoyer ce que les tests créent.
 */
export async function connectToDatabase(): Promise<Client> {
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
  return client;
}
