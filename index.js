const octokit = require("@octokit/core");

const client = new octokit.Octokit({ auth: process.env.GH_TOKEN });
async function updateReadme() {
  try {
    const res = await client.request('GET /repos/{owner}/{repo}/contents/{path}', {
      owner: 'propactive',
      repo: 'propactive',
      path: 'README.md'
    })

    const { path, sha, content, encoding } = res.data;
    const rawContent = Buffer.from(content, encoding).toString('utf-8');
    const regex = /[0-9]?\.[0-9]?\.[0-9]?("|\))*$/gm;
    const updateContent = rawContent.replace(regex, process.env.NEW_VER)
    commitNewReadme(path, sha, encoding, updateContent);
  }
  catch (err) {
    console.log(err);
  }
}

async function commitNewReadme(path, sha, encoding, updateContent) {
  try {
    await client.request('PUT /repos/{owner}/{repo}/contents/{path}', {
      owner: 'propactive',
      repo: 'propactive',
      path: 'README.md',
      message: 'Update Readme with the updated tags',
      content: Buffer.from(updateContent, 'utf-8').toString(encoding),
      path,
      sha,
    });
  } catch (err) {
    console.log(err);
  }
}


updateReadme();
