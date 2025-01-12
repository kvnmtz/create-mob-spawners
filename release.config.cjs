/**
 * @type {import('semantic-release').GlobalConfig}
 */
module.exports = {
  branches: ['main'],
  plugins: [
    '@semantic-release/commit-analyzer',
    '@semantic-release/release-notes-generator',
    './update-version.js',
    [
      '@semantic-release/git',
      {
        assets: ['gradle.properties'],
        message: 'chore(release): update version for ${nextRelease.version} [skip ci]',
      },
    ],
    [
      '@semantic-release/exec',
      {
        prepareCmd: './build.sh ${nextRelease.version}',
      },
    ],
    [
      '@semantic-release/github',
      {
        'assets': [
          {
            'path': 'build/reobfJar/create-mob-spawners-1.20.1-*.jar',
          },
        ],
      },
    ],
    './create_version_modrinth.js',
  ],
};
