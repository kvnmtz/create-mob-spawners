/**
 * @type {import('semantic-release').GlobalConfig}
 */
module.exports = {
  branches: ['main'],
  plugins: [
    '@semantic-release/commit-analyzer',
    '@semantic-release/release-notes-generator',
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
    './create_version_curseforge.js',
  ],
};
