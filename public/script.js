let totalLikes = 0;
let totalComments = 0;

document.getElementById('postForm').addEventListener('submit', function(event) {
    event.preventDefault();

    const messageInput = this.querySelector('input[name="message"]');
    const imageInput = this.querySelector('input[name="image"]');

    const newPost = document.createElement('li');
    newPost.setAttribute('data-likes', '0');
    newPost.setAttribute('data-timestamp', Date.now());

    const postContent = `
        <strong>User1:</strong> ${messageInput.value} <br>
        <img src="${URL.createObjectURL(imageInput.files[0])}" alt="Post Image" style="max-width: 200px;"/> <br>
        <button class="like-button">❤️ Like</button>
        <span class="likes-count">0 Likes</span>
        <button class="share-button">🔗 Share</button>
        <div class="comment-section">
            <input type="text" placeholder="Add a comment..." class="comment-input">
            <button class="comment-button">Comment</button>
            <ul class="comment-list"></ul>
        </div>
        <button class="delete-post-button">Delete Post</button>
    `;
    newPost.innerHTML = postContent;
    document.getElementById('postItems').prepend(newPost);

    messageInput.value = '';
    imageInput.value = '';

    updatePostCount();
    attachEventListeners(newPost);
});

function attachEventListeners(postElement) {
    postElement.querySelector('.like-button').addEventListener('click', function() {
        let likes = parseInt(postElement.getAttribute('data-likes')) + 1;
        postElement.setAttribute('data-likes', likes);
        this.nextElementSibling.innerText = `${likes} Likes`;

        totalLikes++;
        updateEngagementRate();
    });

    postElement.querySelector('.comment-button').addEventListener('click', function() {
        const commentText = this.previousElementSibling.value;
        if (commentText) {
            const commentList = this.nextElementSibling;
            const newComment = document.createElement('li');
            newComment.innerText = commentText;
            commentList.appendChild(newComment);
            this.previousElementSibling.value = '';

            totalComments++;
            updateEngagementRate();
        }
    });

    postElement.querySelector('.delete-post-button').addEventListener('click', function() {
        postElement.remove();
        updatePostCount();
    });

    postElement.querySelector('.share-button').addEventListener('click', function() {
        document.getElementById('shareModal').style.display = 'block';
        document.getElementById('shareLink').value = `https://socialmediaapp.com/post/${postElement.getAttribute('data-timestamp')}`;
    });
}

document.getElementById('sortPosts').addEventListener('change', function() {
    const posts = Array.from(document.getElementById('postItems').children);
    const sortOption = this.value;

    posts.sort((a, b) => {
        if (sortOption === 'newest') return b.getAttribute('data-timestamp') - a.getAttribute('data-timestamp');
        if (sortOption === 'oldest') return a.getAttribute('data-timestamp') - b.getAttribute('data-timestamp');
        if (sortOption === 'mostLikes') return b.getAttribute('data-likes') - a.getAttribute('data-likes');
    });

    document.getElementById('postItems').innerHTML = '';
    posts.forEach(post => document.getElementById('postItems').appendChild(post));
});

document.getElementById('searchPosts').addEventListener('input', function() {
    const keyword = this.value.toLowerCase();
    const posts = document.querySelectorAll('#postItems li');
    posts.forEach(post => {
        const message = post.querySelector('strong').nextSibling.textContent.toLowerCase();
        post.style.display = message.includes(keyword) ? '' : 'none';
    });
});

function updatePostCount() {
    document.getElementById('totalPosts').innerText = document.getElementById('postItems').children.length;
    updateEngagementRate();
}

function updateEngagementRate() {
    const totalPosts = parseInt(document.getElementById('totalPosts').innerText);
    const engagementRate = ((totalLikes + totalComments) / totalPosts) * 100 || 0;
    document.getElementById('engagementRate').innerText = `${engagementRate.toFixed(2)}%`;
    document.getElementById('totalLikes').innerText = totalLikes;
    document.getElementById('totalComments').innerText = totalComments;
}

document.getElementById('toggle-dark-mode').addEventListener('click', function() {
    document.body.classList.toggle('dark-mode');
});

document.getElementById('editProfileButton').addEventListener('click', function() {
    document.getElementById('editProfile').style.display = 'block';
});

document.getElementById('cancelProfileButton').addEventListener('click', function() {
    document.getElementById('editProfile').style.display = 'none';
});

document.getElementById('saveProfileButton').addEventListener('click', function() {
    const newUsername = document.getElementById('newUsername').value;
    document.getElementById('username').textContent = newUsername || "User1";
    document.getElementById('editProfile').style.display = 'none';
});

document.getElementById('closeShareModal').addEventListener('click', function() {
    document.getElementById('shareModal').style.display = 'none';
});
