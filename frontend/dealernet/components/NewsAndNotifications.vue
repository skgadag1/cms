<template>
    <div class="container py-5">
        <div class="row">
            <div class="col-8 pe-4">
                <ComponentHeader title="News For You" counter="3" :link="{label: 'View all News', url: '#'}" />

                <div class="card bg-white">
                    <div class="row g-0">
                        <div class="col-md-4">
                            <svg class="img-fluid rounded-start h-100" width="100%" height="240" xmlns="http://www.w3.org/2000/svg" role="img" preserveAspectRatio="xMidYMid slice" focusable="false"><title>Placeholder</title><rect width="100%" height="100%" fill="#c3c3c3"></rect></svg>
                        </div>
                        <div class="col-md-8">
                            <div class="card-body">
                                <h5 class="card-title">March Sales Programs Announced</h5>
                                <p class="text-muted font-weight-bold">Feb 11, 2023</p>
                                <p><span class="badge rounded-4 bg-dark">Sales</span> <span class="badge rounded-4 bg-dark">Sales Program</span></p>
                                <p class="card-text">Starting March 1, customers can now receive a rebate on all cash purchases of Zero-Turn Mowers.</p>
                                <a href="#" class="link link-primary font-weight-bold">
                                    Read More
                                    <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" fill="currentColor" class="bi bi-chevron-right" viewBox="0 0 16 16">
                                        <path fill-rule="evenodd" d="M4.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L10.293 8 4.646 2.354a.5.5 0 0 1 0-.708z"/>
                                    </svg>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card bg-white mt-3">
                    <div class="row g-0">
                        <div class="col-md-4">
                            <svg class="img-fluid rounded-start h-100" width="100%" height="240" xmlns="http://www.w3.org/2000/svg" role="img" preserveAspectRatio="xMidYMid slice" focusable="false"><title>Placeholder</title><rect width="100%" height="100%" fill="#c3c3c3"></rect></svg>
                        </div>
                        <div class="col-md-8">
                            <div class="card-body">
                                <h5 class="card-title">New Compact Tractor Attachments Announced</h5>
                                <p class="text-muted font-weight-bold">Feb 9, 2023</p>
                                <p><span class="badge rounded-4 bg-dark">Sales</span> <span class="badge rounded-4 bg-dark">Products</span></p>
                                <p class="card-text">Have you ever wanted to grill rotisserie chicken while you do your landscaping? Well, now you can! With the new Grill.... </p>
                                <a href="#" class="link link-primary font-weight-bold">
                                    Read More
                                    <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" fill="currentColor" class="bi bi-chevron-right" viewBox="0 0 16 16">
                                        <path fill-rule="evenodd" d="M4.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L10.293 8 4.646 2.354a.5.5 0 0 1 0-.708z"/>
                                    </svg>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card bg-white mt-3">
                    <div class="row g-0">
                        <div class="col-md-4">
                            <svg class="img-fluid rounded-start h-100" width="100%" height="240" xmlns="http://www.w3.org/2000/svg" role="img" preserveAspectRatio="xMidYMid slice" focusable="false"><title>Placeholder</title><rect width="100%" height="100%" fill="#c3c3c3"></rect></svg>
                        </div>
                        <div class="col-md-8">
                            <div class="card-body">
                                <h5 class="card-title">An Update on Autonomous Loaders</h5>
                                <p class="text-muted font-weight-bold">Feb 7, 2023</p>
                                <p><span class="badge rounded-4 bg-dark">Sales</span> <span class="badge rounded-4 bg-dark">Products</span></p>
                                <p class="card-text">One of them became sentient at the factory and we’re not sure of its intentions.</p>
                                <a href="#" class="link link-primary font-weight-bold">
                                    Read More
                                    <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" fill="currentColor" class="bi bi-chevron-right" viewBox="0 0 16 16">
                                        <path fill-rule="evenodd" d="M4.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L10.293 8 4.646 2.354a.5.5 0 0 1 0-.708z"/>
                                    </svg>
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-4">
                <ComponentHeader title="Notifications" counter="8" :link="{label: 'View all', url: '#'}" />

                <div class="accordion accordion-light" id="accordionNotifications1">
                    <div class="accordion-item">
                        <h5 class="accordion-header">
                            <button class="accordion-button" type="button" data-bs-toggle="collapse" data-bs-target="#accordionSales" aria-expanded="true" aria-controls="accordionSales">
                                Sales <span class="badge bg-primary rounded-circle ms-2">4</span>
                            </button>
                        </h5>
                        <div id="accordionSales" class="accordion-collapse collapse show bg-white" data-bs-parent="#accordionNotifications1">
                            <div class="accordion-body py-0">
                                <ul class="list-group list-group-flush">
                                    <li class="list-group-item bg-white px-0">Order Shipped <span class="badge bg-primary rounded-circle ms-2">1</span></li>
                                    <li class="list-group-item bg-white px-0">Category <span class="badge bg-primary rounded-circle ms-2">2</span></li>
                                    <li class="list-group-item bg-white px-0">Refund Accepted <span class="badge bg-primary rounded-circle ms-2">1</span></li>
                                    <li class="list-group-item bg-white d-inline-flex align-items-center justify-content-center">
                                        <a href="#" class="font-weight-bold">
                                            Go to Sales
                                            <svg xmlns="http://www.w3.org/2000/svg" width="10" height="10" fill="currentColor" class="bi bi-box-arrow-up-right ms-1" viewBox="0 0 16 16">
                                                <path fill-rule="evenodd" d="M8.636 3.5a.5.5 0 0 0-.5-.5H1.5A1.5 1.5 0 0 0 0 4.5v10A1.5 1.5 0 0 0 1.5 16h10a1.5 1.5 0 0 0 1.5-1.5V7.864a.5.5 0 0 0-1 0V14.5a.5.5 0 0 1-.5.5h-10a.5.5 0 0 1-.5-.5v-10a.5.5 0 0 1 .5-.5h6.636a.5.5 0 0 0 .5-.5z"/>
                                                <path fill-rule="evenodd" d="M16 .5a.5.5 0 0 0-.5-.5h-5a.5.5 0 0 0 0 1h3.793L6.146 9.146a.5.5 0 1 0 .708.708L15 1.707V5.5a.5.5 0 0 0 1 0v-5z"/>
                                            </svg>
                                        </a>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="accordion accordion-light" id="accordionNotifications2">
                    <div class="accordion-item">
                        <h5 class="accordion-header">
                            <button class="accordion-button bg-white text-black shadow-none" type="button" data-bs-toggle="collapse" data-bs-target="#accordionDmiq" aria-expanded="true" aria-controls="accordionDmiq">
                                DMIQ <span class="badge bg-primary rounded-circle ms-2">3</span>
                            </button>
                        </h5>
                        <div id="accordionDmiq" class="accordion-collapse collapse show bg-white" data-bs-parent="#accordionNotifications2">
                            <div class="accordion-body py-0">
                                <ul class="list-group list-group-flush">
                                    <li class="list-group-item bg-white px-0">Subscription Expiring <span class="badge bg-primary rounded-circle ms-2">1</span></li>
                                    <li class="list-group-item bg-white px-0">Critical Fault <span class="badge bg-primary rounded-circle ms-2">2</span></li>
                                    <li class="list-group-item bg-white px-0">Geofence Breach <span class="badge bg-primary rounded-circle ms-2">1</span></li>
                                    <li class="list-group-item bg-white d-inline-flex align-items-center justify-content-center">
                                        <a href="#" class="font-weight-bold">
                                            Go to DMIQ
                                            <svg xmlns="http://www.w3.org/2000/svg" width="10" height="10" fill="currentColor" class="bi bi-box-arrow-up-right ms-1" viewBox="0 0 16 16">
                                                <path fill-rule="evenodd" d="M8.636 3.5a.5.5 0 0 0-.5-.5H1.5A1.5 1.5 0 0 0 0 4.5v10A1.5 1.5 0 0 0 1.5 16h10a1.5 1.5 0 0 0 1.5-1.5V7.864a.5.5 0 0 0-1 0V14.5a.5.5 0 0 1-.5.5h-10a.5.5 0 0 1-.5-.5v-10a.5.5 0 0 1 .5-.5h6.636a.5.5 0 0 0 .5-.5z"/>
                                                <path fill-rule="evenodd" d="M16 .5a.5.5 0 0 0-.5-.5h-5a.5.5 0 0 0 0 1h3.793L6.146 9.146a.5.5 0 1 0 .708.708L15 1.707V5.5a.5.5 0 0 0 1 0v-5z"/>
                                            </svg>
                                        </a>
                                    </li>
                                </ul>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>