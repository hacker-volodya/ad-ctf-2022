import { Component, OnInit } from '@angular/core';
import {Droplet} from "../droplet";
import {DropletService} from "../droplet.service";

@Component({
  selector: 'app-droplets',
  templateUrl: './droplets.component.html',
  styleUrls: ['./droplets.component.css']
})
export class DropletsComponent implements OnInit {
  droplets: Droplet[] = [];
  selectedDroplet?: Droplet;

  constructor(private dropletService: DropletService) { }

  ngOnInit(): void {
    this.getDroplets();
  }

  getDroplets(): void {
    this.dropletService.getList().subscribe(droplets => this.droplets = droplets);
  }

  onSelect(droplet: Droplet): void {
    this.dropletService.getOne(droplet.name).subscribe(droplet => this.selectedDroplet = droplet);
  }
}
