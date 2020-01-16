var express = require('express');
var mongodb = require('mongodb');
var router = express.Router();
/* GET home page. */
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express' });
});


router.get('/api/v1/hello', function (req, res) {
  return res.json({'message':'hello world'});
});

function sleep(delay) {
  var start = new Date().getTime();
  while (new Date().getTime() < start + delay)
    ;
}

function prime(n){
  var num=0;
  var j;
  var sgin = false;
  for (let i = 2; i <= n; i++) {
    if(i % 2 === 0 && i !== 2  )  continue; //偶数和1排除

    sgin= true;
    for (j = 2; j <= Math.sqrt(i) ; j++) {
      if (i % j === 0) {
        sgin = false;
        break;
      }
    }

    //打印
    if (sgin) {
      num++;
    }
  }
  console.info(n+"以内的素数有"+num+"个");
}

router.get('/api/v1/compute', async function (req, res) {
  let x = 0, y = 1;
  // let sleepTime = Math.floor(Math.random()*2 );
  prime(10000);
  return res.json({ status: 'done'});
});

router.get('/api/v1/listQuestionaire', async function (req, res) {
  const rs = await db.collection('answers').find({}, {projection:{_id:1, openid:1, user_id:1, suggestion_id:1}}).limit(100).toArray();
  return res.json(rs);
});

let db;
async function getMongoClient() {
  try {
    const mongoUri = 'mongodb://ec2-52-81-100-48.cn-north-1.compute.amazonaws.com.cn:27017/test';
    console.log(mongoUri)
    // ec2-52-81-100-48.cn-north-1.compute.amazonaws.com.cn
    cliect = await mongodb.connect(mongoUri);
    db = cliect.db('test')
  } catch (e) {
    console.log('connect mongo error', e)
  }
}
getMongoClient().then(()=>{
  console.log('mongodb connected')
});


module.exports = router;
