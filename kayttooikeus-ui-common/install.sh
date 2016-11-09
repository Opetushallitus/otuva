#/bin/bash

pwd=`pwd`
NODE_UI_PROJECT="../../kayttooikeus-ui-list"
NODE="$NODE_UI_PROJECT/node/node"
NPM="$NODE_UI_PROJECT/node/node_modules/npm/bin/npm-cli.js"
NODE_MODULES="$NODE_UI_PROJECT/node_modules"

function install {
	echo "Installing $1..."
	cd $1 && $NODE $NPM install \
		&& $NODE $NPM run build \
		&& git add index.js \
		&& echo "Installed $1"
	cd $pwd
}

install "modal" \
	&& install "button" \
	&& install "sort-by-header"
